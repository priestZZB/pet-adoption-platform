package com.pet.module.volunteer.service.impl;

import com.github.pagehelper.PageHelper;
import com.pet.common.enums.ResultCodeEnum;
import com.pet.common.exception.BusinessException;
import com.pet.module.volunteer.mapper.VisitRecordMapper;
import com.pet.module.volunteer.model.dto.VisitDto;
import com.pet.module.volunteer.model.entity.VisitRecord;
import com.pet.module.volunteer.model.vo.VisitVo;
import com.pet.module.volunteer.service.VisitService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VisitServiceImpl implements VisitService {

    @Autowired
    private VisitRecordMapper visitRecordMapper;

    @Override
    public void add(Long volunteerId, VisitDto dto) {
        VisitRecord record = new VisitRecord();
        record.setVolunteerId(volunteerId);
        record.setPetId(dto.getPetId());
        record.setVisitDate(LocalDate.parse(dto.getVisitDate()));
        record.setContent(dto.getContent());
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            record.setImages(String.join(",", dto.getImages()));
        }
        visitRecordMapper.insert(record);
    }

    @Override
    public List<VisitVo> getMyVisits(Long volunteerId, int page, int size) {
        PageHelper.startPage(page, size);
        List<VisitRecord> list = visitRecordMapper.selectByVolunteerId(volunteerId);
        return list.stream().map(this::convertToVo).collect(Collectors.toList());
    }

    @Override
    public VisitVo getVisitDetail(Long id) {
        VisitRecord record = visitRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCodeEnum.BAD_REQUEST, "走访记录不存在");
        }
        return convertToVo(record);
    }

    private VisitVo convertToVo(VisitRecord record) {
        VisitVo vo = new VisitVo();
        BeanUtils.copyProperties(record, vo);
        if (record.getImages() != null && !record.getImages().isEmpty()) {
            vo.setImages(Arrays.asList(record.getImages().split(",")));
        } else {
            vo.setImages(Collections.emptyList());
        }
        return vo;
    }
}